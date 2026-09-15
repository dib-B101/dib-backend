local current_hash = redis.call('HGET', KEYS[1], 'tokenHash')
if not current_hash then
    return 0
end
local member_id = redis.call('HGET', KEYS[1], 'memberId')

redis.call('DEL', KEYS[1])

if member_id then
    local index_key = ARGV[2] .. member_id
    redis.call('SREM', index_key, KEYS[1])
    if redis.call('SCARD', index_key) == 0 then
        redis.call('DEL', index_key)
    end
end

local lookup_key = ARGV[1] .. current_hash
local lookup_ttl = redis.call('TTL', lookup_key)
if lookup_ttl > 0 then
    redis.call('HSET', lookup_key, 'status', 'REVOKED')
    redis.call('EXPIRE', lookup_key, lookup_ttl)
end

return 1
