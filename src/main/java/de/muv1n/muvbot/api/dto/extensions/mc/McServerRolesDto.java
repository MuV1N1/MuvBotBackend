package de.muv1n.muvbot.api.dto.extensions.mc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class McServerRolesDto {
    private List<String> roleIds;
}
