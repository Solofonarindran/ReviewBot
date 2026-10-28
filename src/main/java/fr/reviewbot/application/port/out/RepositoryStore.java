package fr.reviewbot.application.port.out;
import fr.reviewbot.domain.model.Repository;

/** Port sortant : enregistrement des dépôts surveillés. */
public interface RepositoryStore {
    /** insère le dépôt si son id est null, sinon le met à jour . Renvoie le dépôt tel qu'enregistré */
    Repository save(Repository repository) ;

}
