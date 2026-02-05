package com.zhukovskiy.web.dao.impl;

import com.zhukovskiy.web.dao.UserDao;
import com.zhukovskiy.web.entity.User;
import com.zhukovskiy.web.entity.UserDaoDto;
import com.zhukovskiy.web.exception.DaoException;
import com.zhukovskiy.web.mapper.EntityMapper;
import com.zhukovskiy.web.mapper.impl.UserMapper;
import com.zhukovskiy.web.pool.ConnectionPool;
import com.zhukovskiy.web.util.PasswordHasher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UserDaoImpl implements UserDao {
    private static final Logger logger = LogManager.getLogger();
    private final EntityMapper<User> userMapper = new UserMapper();

    private static final String SELECT_PASSWORD_WHERE_LOGIN = "SELECT passwordHash FROM users WHERE login = ?";
    private static final String FIND_ALL = "SELECT * FROM USERS";
    private static final String FIND_BY_LOGIN = "SELECT * FROM users WHERE login = ?";
    private static final String CREATE = "INSERT INTO users(login, passwordHash, role) VALUES(?, ?, ?)";
    private static final String FIND_BY_ID = "SELECT * FROM users WHERE Id = ?";
    private static final String UPDATE = "UPDATE users SET login = ?, passwordHash = ?, role = ? WHERE id = ?";
    private static final String DELETE_BY_ID = "DELETE FROM users WHERE Id = ?";


    private static final UserDaoImpl instance = new UserDaoImpl();

    private UserDaoImpl() {
    }

    public static UserDaoImpl getInstance() {
        return instance;
    }

    @Override
    public List<User> findAll() throws DaoException {
        List<User> users = new ArrayList<>();
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL)) {
            while (resultSet.next()) {
                User user = userMapper.map(resultSet);
                users.add(user);
            }
            logger.info("Found {} users", users.size());
            return users;
        } catch (SQLException e) {
            logger.error("Error finding all users");
            throw new DaoException("Error finding all users" + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByLogin(String login) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_LOGIN)) {
            statement.setString(1, login);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = userMapper.map(resultSet);
                    logger.debug("Found user by login:{}, Id = {}", login, user.getId());
                    return Optional.of(user);
                } else {
                    logger.debug("No users found with login:{}", login);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by login {}: {}", login, e.getMessage());
            throw new DaoException("Error finding user by login: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean create(User user) throws DaoException {
        UserDaoDto dto = user.toDaoDto();
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, dto.login());
            statement.setString(2, dto.passwordHash());
            statement.setString(3, dto.role().name());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                logger.warn("INSERT affected 0 rows for user:{}", user.getLogin());
                return false;
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    logger.warn("No generated keys returned for user: {}", user.getLogin());
                    return true;
                }
                int generatedId = generatedKeys.getInt(1);
                if (generatedId <= 0) {
                    logger.error("Invalid generated ID: {} for user: {}", generatedId, user.getLogin());
                    throw new DaoException("Invalid ID");
                }
                user.setId(generatedId);
            }
            logger.info("Created user with id: {}", user.getId());
            return true;
        } catch (SQLException e) {
            logger.error("Error creating user '{}': {}", user.getLogin(), e.getMessage());
            throw new DaoException("Error creating user" + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(int id) throws DaoException {
        if (id <= 0) {
            logger.warn("Invalid id provided for findById:{}", id);
            return Optional.empty();
        }
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = userMapper.map(resultSet);
                    logger.debug("Found user by id{}:{}", user.getId(), user.getLogin());
                    return Optional.of(user);
                } else {
                    logger.debug("No users found with id:{}", id);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by id {}: {}", id, e.getMessage());
            throw new DaoException("Error finding user by id: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(User user) throws DaoException {
        if (user == null) {
            logger.warn("Attempting to delete null user");
            throw new IllegalArgumentException("User cannot be null");
        }
        int userId = user.getId();
        logger.info("Attempting to delete user: id={}, login={}, role={}",
                userId, user.getLogin(), user.getRole());
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            statement.setInt(1, userId);
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Successfully deleted user: id={}, login={}",
                        userId, user.getLogin());
                return true;
            } else {
                logger.warn("User not found for deletion:id={}, login={}", userId, user.getLogin());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error deleting user:id = {}, login = {}", userId, user.getLogin());
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new DaoException("Cannot delete user due to referential integrity: " +
                        user.getLogin(), e);
            }
            throw new DaoException(String.format("Error deleting user id=%d, login=%s", userId, user.getLogin()), e);
        }
    }

    @Override
    public boolean update(User user) throws DaoException {
        if (user == null) {
            logger.warn("Attempt to update null user");
            throw new IllegalArgumentException("User cannot be null");
        }
        int userId = user.getId();
        Optional<User> optionalUser = findById(userId);
        if (optionalUser.isEmpty()) {
            logger.warn("Cannot update user with Id : {} does not exist", userId);
            return false;
        }

        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            UserDaoDto dto = user.toDaoDto();
            statement.setString(1, user.getLogin());
            statement.setString(2, dto.passwordHash());
            statement.setString(3, dto.role().name());
            statement.setInt(4, dto.id());

            int affectedRows = statement.executeUpdate();
            boolean updated = affectedRows > 0;
            if (updated) {
                logger.info("Successfully updated user: id={}, login={}", userId, user.getLogin());
            } else {
                logger.warn("User ID {} existed but was not updated (no changes?)", userId);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Error updating user id={}, login={}: {}",
                    userId, user.getLogin(), e.getMessage());
            throw new DaoException("Error updating user: " + e.getMessage(), e);
        }
    }

    /*@Override
    public boolean authenticate(String login, String password) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_PASSWORD_WHERE_LOGIN)) {
            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String passwordHashFromDb = resultSet.getString(1);
                    return PasswordHasher.checkPassword(password, passwordHashFromDb);
                }
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error authenticating user: login = {}, {}", login, e.getMessage());
            throw new DaoException("Error authenticating user: " + e.getMessage(), e);
        }
    }*/
}