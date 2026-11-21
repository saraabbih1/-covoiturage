package ma.youcode.covoiturage.service;

import ma.youcode.covoiturage.dto.PassagerRequest;
import ma.youcode.covoiturage.dto.PassagerResponse;

import java.util.List;

public interface PassagerService {

    PassagerResponse creer(PassagerRequest request);

    List<PassagerResponse> lister();

    PassagerResponse consulter(Long id);

    PassagerResponse modifier(Long id, PassagerRequest request);

    List<PassagerResponse> rechercher(String email);
}