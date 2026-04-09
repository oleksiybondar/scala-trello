import type { ReactElement } from "react";

import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import List from "@mui/material/List";
import ListItemButton from "@mui/material/ListItemButton";
import ListItemText from "@mui/material/ListItemText";
import { NavLink, useLocation, useParams } from "react-router-dom";

import { boardSettingsNavItems } from "@components/boards/board-settings/types";

export const BoardSettingsSidebar = (): ReactElement => {
  const location = useLocation();
  const { boardId = "" } = useParams();

  return (
    <Card variant="outlined">
      <CardContent>
        <List aria-label="Board settings sections" disablePadding>
          {boardSettingsNavItems.map(item => {
            const to = "/boards/" + boardId + "/settings/" + item.section;

            return (
              <ListItemButton
                component={NavLink}
                key={item.section}
                selected={location.pathname === to}
                sx={{ borderRadius: "15px" }}
                to={to}
              >
                <ListItemText
                  primary={item.label}
                  slotProps={{
                    primary: {
                      fontWeight: 600
                    }
                  }}
                />
              </ListItemButton>
            );
          })}
        </List>
      </CardContent>
    </Card>
  );
};
