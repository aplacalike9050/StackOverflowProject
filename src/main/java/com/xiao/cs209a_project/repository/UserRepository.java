package com.xiao.cs209a_project.repository;

import com.xiao.cs209a_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT u FROM User u WHERE u.displayName LIKE %:name%")
    List<User> findByDisplayNameContaining(@Param("name") String name);

    @Query("SELECT u FROM User u WHERE u.reputation > :minReputation ORDER BY u.reputation DESC")
    List<User> findUsersWithHighReputation(@Param("minReputation") int minReputation);

    @Query("SELECT u FROM User u ORDER BY u.reputation DESC")
    List<User> findAllOrderByReputationDesc();

    @Query("SELECT COUNT(u) FROM User u WHERE u.reputation > 1000")
    long countHighReputationUsers();

    // 修复：u.createdAt 改为 u.creationDate，类型改为 Long
    @Query("SELECT u FROM User u WHERE u.creationDate > :sinceDate")
    List<User> findUsersCreatedAfter(@Param("sinceDate") Long sinceDate);
}