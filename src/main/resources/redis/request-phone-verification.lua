local requested_at = redis.call('HGET', KEYS[1], 'requestedAt')
if requested_at then
    local elapsed = tonumber(ARGV[3]) - tonumber(requested_at)
    local resend_delay = tonumber(ARGV[5])
    if elapsed < resend_delay then
        return resend_delay - elapsed
    end
end

local request_count = redis.call('INCR', KEYS[2])
if request_count == 1 then
    redis.call('EXPIRE', KEYS[2], tonumber(ARGV[7]))
end
if request_count > tonumber(ARGV[6]) then
    local retry_after = redis.call('TTL', KEYS[2])
    return -math.max(retry_after, 1)
end

redis.call('HSET', KEYS[1],
    'verificationId', ARGV[1],
    'codeHash', ARGV[2],
    'attempts', '0',
    'requestedAt', ARGV[3])
redis.call('EXPIRE', KEYS[1], tonumber(ARGV[4]))
return 0
