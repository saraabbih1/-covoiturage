package ma.youcode.covoiturage.mapper;

import ma.youcode.covoiturage.dto.TrajetResponse;
import ma.youcode.covoiturage.entity.Trajet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = PointArretMapper.class
)
public interface TrajetMapper {

    @Mapping(target = "conducteurId", source = "conducteur.id")
    @Mapping(target = "vehiculeId", source = "vehicule.id")
    TrajetResponse toResponse(Trajet trajet);
}