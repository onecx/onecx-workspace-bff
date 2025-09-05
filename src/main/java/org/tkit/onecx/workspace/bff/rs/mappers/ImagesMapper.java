package org.tkit.onecx.workspace.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ImageInfoDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.MimeTypeDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.RefTypeDTO;
import gen.org.tkit.onecx.workspace.client.model.ImageInfo;
import gen.org.tkit.onecx.workspace.client.model.MimeType;
import gen.org.tkit.onecx.workspace.client.model.RefType;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ImagesMapper {

    ImageInfoDTO map(ImageInfo image);

    ImageInfo map(ImageInfoDTO image);

    @ValueMapping(source = "LOGO_SMALL", target = "LOGO_MINUS_SMALL")
    RefType map(RefTypeDTO refType);

    default MimeType mapMimeType(MimeTypeDTO mimeType) {
        if (mimeType == null) {
            return null;
        }
        return switch (mimeType) {
            case IMAGE_X_ICON -> MimeType.IMAGE_SLASH_X_MINUS_ICON;
            case IMAGE_PNG -> MimeType.IMAGE_SLASH_PNG;
            case IMAGE_JPG -> MimeType.IMAGE_SLASH_JPG;
            case IMAGE_JPEG -> MimeType.IMAGE_SLASH_JPEG;
            case IMAGE_SVG_XML -> MimeType.IMAGE_SLASH_SVG_PLUS_XML;
        };
    }

}
