type AsyncCallback = () => Promise<void> | void;
type GuardCallback = () => boolean;

type VerifyCallback<TResponse, TVerified> = (
  response: TResponse
) => Promise<TVerified> | TVerified;

type SuccessCallback<TVerified> = (
  value: TVerified
) => Promise<void> | void;

type ErrorCallback = (error: unknown) => Promise<void> | void;

/**
 * Fluent builder for submit-like async flows.
 *
 * The intent is to keep UI code focused on business-specific pieces:
 * "when should this run", "what request should be made", "what counts as a valid
 * response", and "what should happen on success or failure".
 *
 * The builder owns the surrounding lifecycle so forms and buttons do not need to
 * repeat the same `try/catch/finally` structure for every async submission path.
 */
interface AsyncSubmitHandlerBuilder<TResponse, TVerified> {
  /**
   * Defines whether the handler should run at all.
   *
   * This is useful for submit preconditions such as validation, dirty checks,
   * or "already submitting" guards.
   *
   * @param callback Returns `true` when execution should continue.
   */
  when: (callback: GuardCallback) => AsyncSubmitHandlerBuilder<TResponse, TVerified>;
  /**
   * Registers logic that runs before the request starts.
   *
   * In UI code this usually clears the previous error state and flips a local
   * loading flag.
   *
   * @param callback Setup callback, typically used for local loading or error state.
   */
  onStart: (callback: AsyncCallback) => AsyncSubmitHandlerBuilder<TResponse, TVerified>;
  /**
   * Registers the main async request.
   *
   * The builder stays transport-agnostic here. The request can be GraphQL, REST,
   * file upload, or any other promise-returning async operation.
   *
   * @param callback Promise-returning request callback.
   */
  request: (
    callback: () => Promise<TResponse>
  ) => AsyncSubmitHandlerBuilder<TResponse, TVerified>;
  /**
   * Validates or transforms the raw response before success handling runs.
   *
   * This is the place to reject incomplete payloads, unwrap nested fields, or map
   * raw transport data into a business-level value.
   *
   * @param callback Response verification callback.
   */
  verify: (
    callback: VerifyCallback<TResponse, TVerified>
  ) => AsyncSubmitHandlerBuilder<TResponse, TVerified>;
  /**
   * Registers logic that runs only after a verified successful response.
   *
   * Typical examples are updating local state, invalidating queries, or resetting
   * a form after a successful mutation.
   *
   * @param callback Success callback.
   */
  onSuccess: (
    callback: SuccessCallback<TVerified>
  ) => AsyncSubmitHandlerBuilder<TResponse, TVerified>;
  /**
   * Registers error handling for any failure in the guarded execution pipeline.
   *
   * Failures can come from the request itself, response verification, or the
   * success callback.
   *
   * @param callback Error callback.
   */
  onError: (callback: ErrorCallback) => AsyncSubmitHandlerBuilder<TResponse, TVerified>;
  /**
   * Registers cleanup logic that always runs after completion.
   *
   * This is usually where temporary UI state such as "submitting" flags is released.
   *
   * @param callback Cleanup callback.
   */
  onFinally: (callback: AsyncCallback) => AsyncSubmitHandlerBuilder<TResponse, TVerified>;
  /**
   * Executes the configured handler pipeline.
   *
   * The returned function is intended to be used directly as a UI event handler,
   * for example `onClick={handler}`.
   *
   * @returns The verified value, or `null` when the guard blocks execution or an error occurs.
   */
  handle: () => Promise<TVerified | null>;
  /**
   * Alias for {@link handle}. Kept for compatibility with the earlier builder API.
   *
   * @returns The verified value, or `null` when the guard blocks execution or an error occurs.
   */
  execute: () => Promise<TVerified | null>;
}

/**
 * Creates a fluent async submit handler builder.
 *
 * @typeParam TResponse Raw response type returned by the async request.
 * @typeParam TVerified Verified or transformed response type passed to `onSuccess`.
 * @returns A chainable builder that produces a reusable submit handler via `handle`.
 */
export const createAsyncSubmitHandler = <
  TResponse,
  TVerified = TResponse
>(): AsyncSubmitHandlerBuilder<TResponse, TVerified> => {
  let guardCallback: GuardCallback = () => true;
  let startCallback: AsyncCallback = () => undefined;
  let requestCallback: (() => Promise<TResponse>) | null = null;
  let verifyCallback: VerifyCallback<TResponse, TVerified> = response =>
    response as unknown as TVerified;
  let successCallback: SuccessCallback<TVerified> = () => undefined;
  let errorCallback: ErrorCallback = error => {
    throw error;
  };
  let finallyCallback: AsyncCallback = () => undefined;

  const handle = async (): Promise<TVerified | null> => {
    try {
      // Guards are evaluated before any side effects run, so ignored submits stay cheap.
      if (!guardCallback()) {
        return null;
      }

      if (requestCallback === null) {
        throw new Error("Async action builder requires a request callback.");
      }

      // Start phase lets the caller enter a consistent "submission in progress" state.
      await startCallback();

      // Request and verification are split so transport concerns stay separate from
      // business rules about what counts as a successful result.
      const response = await requestCallback();
      const verified = await verifyCallback(response);
      await successCallback(verified);

      return verified;
    } catch (error) {
      // All failures are normalized through one error hook so UI code has a single
      // place to translate exceptions into user-facing state.
      await errorCallback(error);

      return null;
    } finally {
      // Cleanup always runs, even if verification or success handling throws.
      await finallyCallback();
    }
  };

  const builder: AsyncSubmitHandlerBuilder<TResponse, TVerified> = {
    handle,
    onError(callback) {
      errorCallback = callback;

      return builder;
    },
    onFinally(callback) {
      finallyCallback = callback;

      return builder;
    },
    onStart(callback) {
      startCallback = callback;

      return builder;
    },
    onSuccess(callback) {
      successCallback = callback;

      return builder;
    },
    when(callback) {
      guardCallback = callback;

      return builder;
    },
    request(callback) {
      requestCallback = callback;

      return builder;
    },
    verify(callback) {
      verifyCallback = callback;

      return builder;
    },
    execute: handle
  };

  return builder;
};

/**
 * Backward-compatible alias for {@link createAsyncSubmitHandler}.
 */
export const createAsyncActionBuilder = createAsyncSubmitHandler;
