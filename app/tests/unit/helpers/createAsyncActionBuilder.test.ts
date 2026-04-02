import {
  createAsyncActionBuilder,
  createAsyncSubmitHandler
} from "@helpers/createAsyncActionBuilder";

describe("createAsyncSubmitHandler", () => {
  test("runs the full success lifecycle in order and returns the verified value", async () => {
    const events: string[] = [];

    const handle = createAsyncSubmitHandler<{ value: number }, number>()
      .when(() => {
        events.push("when");

        return true;
      })
      .onStart(() => {
        events.push("start");
      })
      .request(async () => {
        events.push("request");

        return {
          value: 7
        };
      })
      .verify(response => {
        events.push("verify");

        return response.value * 2;
      })
      .onSuccess(value => {
        events.push(`success:${String(value)}`);
      })
      .onError(() => {
        events.push("error");
      })
      .onFinally(() => {
        events.push("finally");
      })
      .handle;

    await expect(handle()).resolves.toBe(14);
    expect(events).toEqual([
      "when",
      "start",
      "request",
      "verify",
      "success:14",
      "finally"
    ]);
  });

  test("returns null when the guard blocks execution and still runs cleanup", async () => {
    const events: string[] = [];

    const handle = createAsyncSubmitHandler<string>()
      .when(() => {
        events.push("when");

        return false;
      })
      .onStart(() => {
        events.push("start");
      })
      .request(async () => {
        events.push("request");

        return "value";
      })
      .onError(() => {
        events.push("error");
      })
      .onFinally(() => {
        events.push("finally");
      })
      .handle;

    await expect(handle()).resolves.toBeNull();
    expect(events).toEqual(["when", "finally"]);
  });

  test("routes request failures through onError and always runs cleanup", async () => {
    const events: string[] = [];

    const handle = createAsyncSubmitHandler<string>()
      .onStart(() => {
        events.push("start");
      })
      .request(async () => {
        events.push("request");
        throw new Error("boom");
      })
      .onError(error => {
        events.push(
          error instanceof Error ? `error:${error.message}` : "error:unknown"
        );
      })
      .onFinally(() => {
        events.push("finally");
      })
      .handle;

    await expect(handle()).resolves.toBeNull();
    expect(events).toEqual(["start", "request", "error:boom", "finally"]);
  });

  test("routes verification failures through onError and skips onSuccess", async () => {
    const events: string[] = [];

    const handle = createAsyncSubmitHandler<{ valid: boolean }>()
      .request(async () => {
        events.push("request");

        return {
          valid: false
        };
      })
      .verify(response => {
        events.push("verify");

        if (!response.valid) {
          throw new Error("invalid");
        }

        return response;
      })
      .onSuccess(() => {
        events.push("success");
      })
      .onError(error => {
        events.push(
          error instanceof Error ? `error:${error.message}` : "error:unknown"
        );
      })
      .onFinally(() => {
        events.push("finally");
      })
      .handle;

    await expect(handle()).resolves.toBeNull();
    expect(events).toEqual(["request", "verify", "error:invalid", "finally"]);
  });

  test("uses execute as an alias for handle", async () => {
    const builder = createAsyncActionBuilder<number>()
      .request(async () => 3)
      .onError(() => undefined);

    await expect(builder.handle()).resolves.toBe(3);
    await expect(builder.execute()).resolves.toBe(3);
  });

  test("treats a missing request callback as a handled failure", async () => {
    const events: string[] = [];

    const handle = createAsyncSubmitHandler<number>()
      .onError(error => {
        events.push(
          error instanceof Error ? `error:${error.message}` : "error:unknown"
        );
      })
      .onFinally(() => {
        events.push("finally");
      })
      .handle;

    await expect(handle()).resolves.toBeNull();
    expect(events).toEqual([
      "error:Async action builder requires a request callback.",
      "finally"
    ]);
  });
});
