/**
 * Reads a browser `File` as a base64 data URL.
 *
 * @param file File selected by the user.
 * @returns Encoded data URL string.
 * @throws Error When the browser fails to read the file or produces a non-string result.
 */
export const readFileAsDataUrl = async (file: File): Promise<string> => {
  return await new Promise((resolve, reject) => {
    const fileReader = new FileReader();

    fileReader.onerror = () => {
      reject(new Error("Failed to read the selected image."));
    };

    fileReader.onload = () => {
      if (typeof fileReader.result !== "string") {
        reject(new Error("Failed to convert the selected image."));
        return;
      }

      resolve(fileReader.result);
    };

    fileReader.readAsDataURL(file);
  });
};
