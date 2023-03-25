package common.validation.validator.signup;

import common.validation.dto.InvalidResponse;
import member.dto.MemberAddDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UsernameValidatorTest {
    private final UsernameValidator validator = new UsernameValidator();

    @Test
    @DisplayName("이름 검증")
    void usernameValidator() {
        //given
        MemberAddDto memberAddDto = MemberAddDto.builder()
                .username("김싸피")
                .build();

        //when
        List<InvalidResponse> validate = validator.validate(memberAddDto);

        //then
        assertThat(validate).isEmpty();
    }

    @ParameterizedTest
    @DisplayName("이름 길이 예외")
    @CsvSource({"김수한무거북이와두루미삼천갑자동방삭치치카포사리사리센타워리워리세브리깡"})
    void exception_length(String username) {
        //given
        MemberAddDto memberAddDto = MemberAddDto.builder()
                .username(username)
                .build();

        //when
        List<InvalidResponse> validate = validator.validate(memberAddDto);

        //then
        assertThat(validate).isNotEmpty();
    }

    @ParameterizedTest
    @DisplayName("이름 타입 예외")
    @CsvSource({"mark", "김싸피1", "김 싸 피", "김싸피!"})
    void exception_type(String username) {
        //given
        MemberAddDto memberAddDto = MemberAddDto.builder()
                .username(username)
                .build();

        //when
        List<InvalidResponse> validate = validator.validate(memberAddDto);

        //then
        assertThat(validate).isNotEmpty();
    }
}