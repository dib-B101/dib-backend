local values = redis.call('HMGET', KEYS[1], 'purpose', 'phoneHash')
if not values[1] or values[1] ~= ARGV[1] or values[2] ~= ARGV[2] then
    return 0
end
return redis.call('DEL', KEYS[1])
