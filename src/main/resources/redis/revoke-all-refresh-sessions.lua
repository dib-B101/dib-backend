local session_keys = redis.call('SMEMBERS', KEYS[1])

for _, session_key in ipairs(session_keys) do
    local current_hash = redis.call('HGET', session_key, 'tokenHash')
    redis.call('DEL', session_key)

    if current_hash then
        local lookup_key = ARGV[1] .. current_hash
        local lookup_ttl = redis.call('TTL', lookup_key)
        if lookup_ttl > 0 then
            redis.call('HSET', lookup_key, 'status', 'REVOKED')
            redis.call('EXPIRE', lookup_key, lookup_ttl)
        end
    end
end

redis.call('DEL', KEYS[1])
return #session_keys
