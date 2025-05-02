package com.example.part35teammonew.domain.user.repository;

import com.example.part35teammonew.config.TestJpaConfig;
import com.example.part35teammonew.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 지금 설정된 실제 데이터 베이스를 사용하도록 설정
@Import(TestJpaConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 회원_저장_성공() {
        // given
        User user = User.create("test@example.com", "tester", "encryptedPw");

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("tester");
        assertThat(savedUser.isDeleted()).isFalse();
    }

    @Test
    void 이메일_중복_저장_예외() {
        // given
        userRepository.save(User.create("test@example.com", "nick1", "pw1"));

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(User.create("test@example.com", "nick2", "pw2"));
        });
    }

    @Test
    void 닉네임_수정_성공() {
        // given
        User user = userRepository.save(User.create("nicktest@example.com", "oldNick", "pw"));

        // when
        user.updateNickname("newNick");
        userRepository.save(user);

        // then
        User updated = userRepository.findById(user.getId()).get();
        assertThat(updated.getNickname()).isEqualTo("newNick");
    }

    @Test
    void 사용자_논리_삭제_성공() {
        // given
        User user = userRepository.save(User.create("del@example.com", "delNick", "pw"));

        // when
        user.deleteLogical();
        userRepository.save(user);

        // then
        User deleted = userRepository.findById(user.getId()).get();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    void 생성_및_수정_시간_확인() throws InterruptedException {
        // given
        User user = userRepository.save(User.create("time@example.com", "timeNick", "pw"));

        // when
        LocalDateTime createdAt = user.getCreatedAt();

        // 약간의 시간 차이를 주기 위해 sleep
        Thread.sleep(10);

        user.updateNickname("newTimeNick");
        userRepository.save(user);

        // then
        User updated = userRepository.findById(user.getId()).get();
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(createdAt);
    }
}