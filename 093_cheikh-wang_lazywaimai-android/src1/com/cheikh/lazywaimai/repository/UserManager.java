package com.cheikh.lazywaimai.repository;

import com.cheikh.lazywaimai.model.bean.User;
import com.cheikh.lazywaimai.util.PreferenceUtil;

public class UserManager {

    private static final Class<User> CLAZZ = User.class;

    private User user;

    public void saveUser(User user) {
        if (user == null) {
            return;
        }
        this.user = user;
        PreferenceUtil.set(CLAZZ.getName(), this.user);
    }

    public User getUser() {
        if (user == null) {
            user = PreferenceUtil.getObject(CLAZZ.getName(), CLAZZ);
        }

        return user;
    }

    public String getToken() {
        if (getUser() != null) {
            return getUser().getToken();
        }
        return null;
    }

    public void clear() {
        user = null;
        PreferenceUtil.set(CLAZZ.getName(), "");
    }
}
