package ma.youcode.covoiturage.mapper;

import ma.youcode.covoiturage.dto.VehiculeRequest;
import ma.youcode.covoiturage.dto.VehiculeResponse;
import ma.youcode.covoiturage.entity.Vehicule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    Vehicule toEntity(VehiculeRequest request);

    @Mapping(target = "conducteurId", source = "conducteur.id")
    VehiculeResponse toResponse(Vehicule vehicule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    void update(
            VehiculeRequest request,
            @MappingTarget Vehicule vehicule
    );
}