package com.b101.dib.auth.query.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthQueryMapper {

    boolean existsByEmail(@Param("email") String email);
}
