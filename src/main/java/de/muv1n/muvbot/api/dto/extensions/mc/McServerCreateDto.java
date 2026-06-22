package de.muv1n.muvbot.api.dto.extensions.mc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class McServerCreateDto {
    private String serverName;
    private String serverIp;
}
