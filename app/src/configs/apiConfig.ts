const trimTrailingSlash = (value: string): string => {
  return value.replace(/\/+$/u, "");
};

const apiBaseUrl = import.meta.env.VITE_API_URL?.trim();

export const buildApiUrl = (path: string): string => {
  if (apiBaseUrl === undefined || apiBaseUrl === "") {
    return path;
  }

  return `${trimTrailingSlash(apiBaseUrl)}${path}`;
};
