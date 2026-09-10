redis.call('HSET', KEYS[1],
    'tokenHash', ARGV[1],
    'familyId', ARGV[2],
    'issuedAt', ARGV[3],
    'lastUsedAt', ARGV[3],
    'absoluteExpiresAt', ARGV[4])
redis.call('EXPIRE', KEYS[1], tonumber(ARGV[5]))
return 1
