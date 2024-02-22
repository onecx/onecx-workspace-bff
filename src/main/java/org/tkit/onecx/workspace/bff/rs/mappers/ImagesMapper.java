package org.tkit.onecx.workspace.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ImageInfoDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.RefTypeDTO;
import gen.org.tkit.onecx.workspace.client.model.ImageInfo;
import gen.org.tkit.onecx.workspace.client.model.RefType;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ImagesMapper {

    ImageInfoDTO map(ImageInfo image);

    ImageInfo map(ImageInfoDTO image);

    RefType map(RefTypeDTO refType);

}
