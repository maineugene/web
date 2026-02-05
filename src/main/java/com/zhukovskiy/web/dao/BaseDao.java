package com.zhukovskiy.web.dao;

import com.zhukovskiy.web.entity.AbstractEntity;
import com.zhukovskiy.web.exception.DaoException;

import java.util.List;
import java.util.Optional;

public interface BaseDao<T extends AbstractEntity> {
    public List<T> findAll() throws DaoException;
    public Optional<T> findByLogin(String login) throws DaoException;
    public boolean create(T t) throws DaoException;
    public Optional<T> findById(int id) throws DaoException;
    public boolean update(T t) throws DaoException;
    public boolean delete(T t) throws DaoException;
}