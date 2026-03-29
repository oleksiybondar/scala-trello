import { createBrowserRouter } from "react-router-dom";

import { HomePage } from "@pages/HomePage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <HomePage />
  },
  {
    path: "/home",
    element: <HomePage />
  }
]);
