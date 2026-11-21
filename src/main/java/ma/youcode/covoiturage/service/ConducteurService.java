package ma.youcode.covoiturage.service;

import ma.youcode.covoiturage.dto.ConducteurRequest;
import ma.youcode.covoiturage.dto.ConducteurResponse;

import java.util.List;

public interface ConducteurService {
    ConducteurResponse creer(ConducteurRequest request);
    List<ConducteurResponse>lister();
    ConducteurResponse consulter(Long id);
    ConducteurResponse modifier(Long id,ConducteurRequest request);
    List<ConducteurResponse>rechercher(String recherche);
}
