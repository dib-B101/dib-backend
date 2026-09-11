local function revoke_current_session(session_key, lookup_prefix)
    if redis.call('EXISTS', session_key) == 0 then
        return
    end

    local current_hash = redis.call('HGET', session_key, 'tokenHash')
    redis.call('DEL', session_key)
    if current_hash then
        local current_lookup_key = lookup_prefix .. current_hash
        local current_lookup_ttl = redis.call('TTL', current_lookup_key)
        if current_lookup_ttl > 0 then
            redis.call('HSET', current_lookup_key, 'status', 'REVOKED')
            redis.call('EXPIRE', current_lookup_key, current_lookup_ttl)
        end
    end
end

if redis.call('EXISTS', KEYS[1]) == 0 then
    return -1
end

local lookup_status = redis.call('HGET', KEYS[1], 'status')
local lookup_session_key = redis.call('HGET', KEYS[1], 'sessionKey')
if lookup_status ~= 'ACTIVE' then
    if lookup_session_key then
        revoke_current_session(lookup_session_key, ARGV[9])
    end
    return -2
end

if lookup_session_key ~= KEYS[3] then
    revoke_current_session(KEYS[3], ARGV[9])
    return -2
end

if redis.call('EXISTS', KEYS[3]) == 0 then
    return -3
end

if redis.call('HGET', KEYS[3], 'tokenHash') ~= ARGV[1] then
    revoke_current_session(KEYS[3], ARGV[9])
    return -4
end

if tonumber(ARGV[5]) <= tonumber(ARGV[4]) then
    redis.call('DEL', KEYS[3])
    return -5
end

if redis.call('HGET', KEYS[3], 'deviceId') ~= ARGV[8] then
    return -6
end

local previous_lookup_ttl = redis.call('TTL', KEYS[1])
redis.call('HSET', KEYS[1], 'status', 'REVOKED')
if previous_lookup_ttl > 0 then
    redis.call('EXPIRE', KEYS[1], previous_lookup_ttl)
end

redis.call('HSET', KEYS[3],
    'tokenHash', ARGV[2],
    'familyId', ARGV[3],
    'issuedAt', ARGV[4],
    'lastUsedAt', ARGV[4],
    'absoluteExpiresAt', ARGV[5])
redis.call('EXPIRE', KEYS[3], tonumber(ARGV[6]))

redis.call('HSET', KEYS[2],
    'sessionKey', KEYS[3],
    'status', 'ACTIVE',
    'absoluteExpiresAt', ARGV[5])
redis.call('EXPIRE', KEYS[2], tonumber(ARGV[7]))

return 1
