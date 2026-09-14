local current_hash = redis.call('HGET', KEYS[1], 'tokenHash')
if not current_hash then
    return 0
end

redis.call('DEL', KEYS[1])

local lookup_key = ARGV[1] .. current_hash
local lookup_ttl = redis.call('TTL', lookup_key)
if lookup_ttl > 0 then
    redis.call('HSET', lookup_key, 'status', 'REVOKED')
    redis.call('EXPIRE', lookup_key, lookup_ttl)
end

return 1
