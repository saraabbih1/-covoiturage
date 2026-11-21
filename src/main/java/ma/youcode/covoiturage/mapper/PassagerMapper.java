package ma.youcode.covoiturage.mapper;

import ma.youcode.covoiturage.dto.PassagerRequest;
import ma.youcode.covoiturage.dto.PassagerResponse;
import ma.youcode.covoiturage.entity.Passager;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PassagerMapper {

    @Mapping(target = "id", ignore = true)
    Passager toEntity(PassagerRequest request);

    PassagerResponse toResponse(Passager passager);

    @Mapping(target = "id", ignore = true)
    void update(
            PassagerRequest request,
            @MappingTarget Passager passager
    );
}