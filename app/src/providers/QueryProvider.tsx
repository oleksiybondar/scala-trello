import type { PropsWithChildren, ReactElement } from "react";
import { useState } from "react";

import {
  QueryClient,
  QueryClientProvider
} from "@tanstack/react-query";

export const QueryProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          mutations: {
            retry: false
          },
          queries: {
            retry: false
          }
        }
      })
  );

  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
};
