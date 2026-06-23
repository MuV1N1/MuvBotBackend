package de.muv1n.muvbot.api.dto.extensions.mc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WhitelistDto {
    private String id;
    private String serverName;
    private String serverIp;
    private String serverVersion;
    private List<String> whitelistRoleIds;
}
