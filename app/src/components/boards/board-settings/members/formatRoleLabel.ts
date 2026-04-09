export const formatRoleLabel = (value: string): string => {
  return value
    .split(/[_\s]+/)
    .filter(part => part.length > 0)
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
};
