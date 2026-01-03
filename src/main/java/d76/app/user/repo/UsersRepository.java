package d76.app.user.repo;

import d76.app.user.entity.Users;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@NullMarked
@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    @EntityGraph(attributePaths = {"roles", "authProviders"})
    Optional<Users> findByUsernameOrEmail(String username, String email);

    @EntityGraph(attributePaths = {"roles", "authProviders"})
    Optional<Users> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
