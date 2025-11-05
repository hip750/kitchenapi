package com.example.kitchenapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AppSecurityProps 単体テスト")
class AppSecurityPropsTest {

    private AppSecurityProps props;

    @BeforeEach
    void setUp() {
        props = new AppSecurityProps();
    }

    @Test
    @DisplayName("jwtSecret - setterとgetterが正しく動作する")
    void jwtSecret_SetterAndGetterWork() {
        // Given
        String secret = "test-secret-key-12345";

        // When
        props.setJwtSecret(secret);

        // Then
        assertThat(props.getJwtSecret()).isEqualTo(secret);
    }

    @Test
    @DisplayName("jwtExpMinutes - setterとgetterが正しく動作する")
    void jwtExpMinutes_SetterAndGetterWork() {
        // Given
        int expMinutes = 120;

        // When
        props.setJwtExpMinutes(expMinutes);

        // Then
        assertThat(props.getJwtExpMinutes()).isEqualTo(expMinutes);
    }

    @Test
    @DisplayName("デフォルト値 - 初期状態ではnullまたは0である")
    void defaultValues_AreNullOrZero() {
        // Then
        assertThat(props.getJwtSecret()).isNull();
        assertThat(props.getJwtExpMinutes()).isZero();
    }

    @Test
    @DisplayName("jwtSecret - null値を設定できる")
    void jwtSecret_CanBeSetToNull() {
        // Given
        props.setJwtSecret("initial-secret");

        // When
        props.setJwtSecret(null);

        // Then
        assertThat(props.getJwtSecret()).isNull();
    }

    @Test
    @DisplayName("jwtSecret - 空文字列を設定できる")
    void jwtSecret_CanBeSetToEmptyString() {
        // Given
        String emptySecret = "";

        // When
        props.setJwtSecret(emptySecret);

        // Then
        assertThat(props.getJwtSecret()).isEmpty();
    }

    @Test
    @DisplayName("jwtExpMinutes - 負の値を設定できる")
    void jwtExpMinutes_CanBeSetToNegative() {
        // Given
        int negativeValue = -10;

        // When
        props.setJwtExpMinutes(negativeValue);

        // Then
        assertThat(props.getJwtExpMinutes()).isEqualTo(negativeValue);
    }

    @Test
    @DisplayName("jwtExpMinutes - 大きな値を設定できる")
    void jwtExpMinutes_CanBeSetToLargeValue() {
        // Given
        int largeValue = 525600; // 1年分の分数

        // When
        props.setJwtExpMinutes(largeValue);

        // Then
        assertThat(props.getJwtExpMinutes()).isEqualTo(largeValue);
    }

    @Test
    @DisplayName("複数回の設定 - 値を上書きできる")
    void multipleSettings_CanOverwriteValues() {
        // Given
        props.setJwtSecret("first-secret");
        props.setJwtExpMinutes(60);

        // When
        props.setJwtSecret("second-secret");
        props.setJwtExpMinutes(120);

        // Then
        assertThat(props.getJwtSecret()).isEqualTo("second-secret");
        assertThat(props.getJwtExpMinutes()).isEqualTo(120);
    }
}
