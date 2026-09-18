package com.b101.dib.member.repository;

import java.util.Optional;

import com.b101.dib.member.domain.SocialAccount;
import com.b101.dib.member.domain.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(
            SocialProvider provider,
            String providerUserId
    );
}
