package backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.pojo.User;

/**
 * UserDAO类继承JpaRepository，就提供了CRUD和分页 的各种常见功能。
 * 自带一些不需实现的接口，见文档https://docs.spring.io/spring-data/jpa/docs/2.4.6/reference/html/#repository-query-keywords Appendix部分
 */
public interface UserDAO extends JpaRepository<User, Integer> {
    User findUserByUsername(String username);
}
