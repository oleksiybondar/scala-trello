import type { ReactElement } from "react";
import { RouterProvider } from "react-router-dom";

import { router } from "@routes/router";

export const AppRouter = (): ReactElement => {
  return <RouterProvider router={router} />;
};
