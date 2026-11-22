package ma.youcode.covoiturage.mapper;

import ma.youcode.covoiturage.dto.PointArretRequest;
import ma.youcode.covoiturage.dto.PointArretResponse;
import ma.youcode.covoiturage.entity.PointArret;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PointArretMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trajet", ignore = true)
    PointArret toEntity(PointArretRequest request);

    PointArretResponse toResponse(PointArret pointArret);
}