import type { ReactElement } from "react";

import { ToolbarSwitchControl } from "@components/toolbar/ToolbarSwitchControl";

interface BoardsShowInactiveToggleProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
}

export const BoardsShowInactiveToggle = ({
  checked,
  onChange
}: BoardsShowInactiveToggleProps): ReactElement => {
  return <ToolbarSwitchControl checked={checked} label="Show inactive" onChange={onChange} />;
};
