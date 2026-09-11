local previous_hash = redis.call('HGET', KEYS[1], 'tokenHash')
if previous_hash and previous_hash ~= ARGV[1] then
    local previous_lookup_key = ARGV[8] .. previous_hash
    local previous_lookup_ttl = redis.call('TTL', previous_lookup_key)
    if previous_lookup_ttl > 0 then
        redis.call('HSET', previous_lookup_key, 'status', 'REVOKED')
        redis.call('EXPIRE', previous_lookup_key, previous_lookup_ttl)
    end
end

redis.call('HSET', KEYS[1],
    'tokenHash', ARGV[1],
    'familyId', ARGV[2],
    'issuedAt', ARGV[3],
    'lastUsedAt', ARGV[3],
    'absoluteExpiresAt', ARGV[4],
    'memberId', ARGV[6],
    'deviceId', ARGV[7])
redis.call('EXPIRE', KEYS[1], tonumber(ARGV[5]))

redis.call('HSET', KEYS[2],
    'sessionKey', KEYS[1],
    'status', 'ACTIVE',
    'absoluteExpiresAt', ARGV[4])
redis.call('EXPIRE', KEYS[2], tonumber(ARGV[9]))
return 1
