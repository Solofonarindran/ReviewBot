package fr.reviewbot.infrastructure.in.web;

/**
 * Réponse de GET /api/status.
 *
 * @param application    nom de l'application
 * @param status         "UP" quand l'application répond
 * @param javaVersion    version de Java utilisée
 * @param virtualThreads vrai si la requête est traitée par une thread virtuelle
 */
public record StatusResponse(String application, String status, String javaVersion, boolean virtualThreads) {
}
