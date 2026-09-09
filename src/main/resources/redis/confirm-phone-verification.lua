local stored_verification_id = redis.call('HGET', KEYS[1], 'verificationId')
if not stored_verification_id or stored_verification_id ~= ARGV[1] then
    return -1
end

local attempts = tonumber(redis.call('HGET', KEYS[1], 'attempts') or '0')
local max_attempts = tonumber(ARGV[3])

if redis.call('HGET', KEYS[1], 'codeHash') ~= ARGV[2] then
    attempts = redis.call('HINCRBY', KEYS[1], 'attempts', 1)
    if attempts >= max_attempts then
        redis.call('DEL', KEYS[1])
        return -2
    end
    return max_attempts - attempts
end

redis.call('DEL', KEYS[1])
redis.call('HSET', KEYS[2],
    'purpose', ARGV[4],
    'phoneHash', ARGV[5],
    'verifiedAt', ARGV[6])
redis.call('EXPIRE', KEYS[2], tonumber(ARGV[7]))
return 0
