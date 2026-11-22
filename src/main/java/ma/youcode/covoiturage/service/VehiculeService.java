package ma.youcode.covoiturage.service;

import ma.youcode.covoiturage.dto.VehiculeRequest;
import ma.youcode.covoiturage.dto.VehiculeResponse;

import java.util.List;

public interface VehiculeService {

    VehiculeResponse creer(
            Long conducteurId,
            VehiculeRequest request
    );

    List<VehiculeResponse> lister(Long conducteurId);

    VehiculeResponse consulter(Long conducteurId, Long vehiculeId);

    VehiculeResponse modifier(
            Long conducteurId,
            Long vehiculeId,
            VehiculeRequest request
    );

    void supprimer(Long conducteurId, Long vehiculeId);
}