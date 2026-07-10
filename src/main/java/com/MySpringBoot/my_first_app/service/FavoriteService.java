package com.MySpringBoot.my_first_app.service;

import com.MySpringBoot.my_first_app.entity.Favorite;
import com.MySpringBoot.my_first_app.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    public boolean addFavorite(Integer userId, Integer bookId) {
        if (favoriteMapper.checkExists(userId, bookId) > 0) return false;
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setBookId(bookId);
        return favoriteMapper.insert(fav) > 0;
    }

    public boolean removeFavorite(Integer id) {
        return favoriteMapper.deleteById(id) > 0;
    }

    public boolean removeByBook(Integer userId, Integer bookId) {
        return favoriteMapper.deleteByUserAndBook(userId, bookId) > 0;
    }

    public List<Favorite> getMyFavorites(Integer userId) {
        return favoriteMapper.findByUserId(userId);
    }

    public boolean checkFavorite(Integer userId, Integer bookId) {
        return favoriteMapper.checkExists(userId, bookId) > 0;
    }
}
