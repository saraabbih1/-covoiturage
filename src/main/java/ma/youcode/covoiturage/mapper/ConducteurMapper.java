package ma.youcode.covoiturage.mapper;

import ma.youcode.covoiturage.dto.ConducteurRequest;
import ma.youcode.covoiturage.dto.ConducteurResponse;
import ma.youcode.covoiturage.entity.Conducteur;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ConducteurMapper {

    @Mapping(target = "id", ignore = true)
    Conducteur toEntity(ConducteurRequest request);

    ConducteurResponse toResponse(Conducteur conducteur);

    @Mapping(target = "id", ignore = true)
    void update(
            ConducteurRequest request,
            @MappingTarget Conducteur conducteur
    );
}