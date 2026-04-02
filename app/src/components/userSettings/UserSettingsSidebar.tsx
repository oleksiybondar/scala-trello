import type { ReactElement } from "react";

import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import List from "@mui/material/List";
import ListItemButton from "@mui/material/ListItemButton";
import ListItemText from "@mui/material/ListItemText";
import { NavLink, useLocation } from "react-router-dom";

import { userSettingsNavItems } from "@components/userSettings/types";

export const UserSettingsSidebar = (): ReactElement => {
  const location = useLocation();

  return (
    <Card variant="outlined">
      <CardContent>
        <List aria-label="User settings sections" disablePadding>
          {userSettingsNavItems.map(item => (
            <ListItemButton
              component={NavLink}
              key={item.section}
              selected={location.pathname === item.to}
              to={item.to}
              sx={{ borderRadius: '15px' }}
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
          ))}
        </List>
      </CardContent>
    </Card>
  );
};
