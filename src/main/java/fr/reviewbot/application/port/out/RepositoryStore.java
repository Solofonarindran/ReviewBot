package fr.reviewbot.application.port.out;
import fr.reviewbot.domain.model.Repository;

public interface RepositoryStore {
    Repository save(Repository repository) ;
}
