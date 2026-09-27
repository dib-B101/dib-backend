if redis.call('EXISTS', KEYS[1]) == 1 then
    return 0
end
redis.call('SET', KEYS[1], '1', 'EX', ARGV[5])
redis.call('HSET', KEYS[2], 'purpose', ARGV[1], 'phoneHash', ARGV[2], 'verifiedAt', ARGV[3])
redis.call('EXPIRE', KEYS[2], ARGV[4])
return 1
