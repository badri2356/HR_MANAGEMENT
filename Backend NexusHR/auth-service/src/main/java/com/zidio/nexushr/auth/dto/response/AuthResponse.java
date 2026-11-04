    package com.zidio.nexushr.auth.dto.response;

    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public class AuthResponse {

        private String accessToken;

        private String refreshToken;

        private String tokenType;

        private Long expiresIn;

    }
