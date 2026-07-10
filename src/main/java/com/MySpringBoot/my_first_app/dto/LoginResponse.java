package com.MySpringBoot.my_first_app.dto;

public class LoginResponse {
    private String token;
    private UserInfo userInfo;

    public static class UserInfo {
        private Integer id;
        private String username;
        private String nickname;
        private String avatar;
        private String role;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserInfo getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfo userInfo) { this.userInfo = userInfo; }
}
