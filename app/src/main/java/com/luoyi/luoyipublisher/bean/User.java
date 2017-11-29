package com.luoyi.luoyipublisher.bean;

public class User {

        private Integer id;

        private String userId;

        private String password;

        private String phone;

        private String nickName;

        private String profile;

        private String physicalPath;

        private String email;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId == null ? null : userId.trim();
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password == null ? null : password.trim();
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName == null ? null : nickName.trim();
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile == null ? null : profile.trim();
        }

        public String getPhysicalPath() {
            return physicalPath;
        }

        public void setPhysicalPath(String physicalPath) {
            this.physicalPath = physicalPath;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email == null ? null : email.trim();
        }
    }