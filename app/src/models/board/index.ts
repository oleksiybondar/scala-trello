export { buildCreateDashboardMutation, buildMyDashboardsQuery } from "./graphql";
export { mapCreateBoardInputToRequest, mapDashboardResponseToBoard } from "./mappers";
export type {
  CreateBoardRequest,
  CreateDashboardMutationResponse,
  DashboardResponse,
  MyDashboardsQueryResponse
} from "./dto";
export type { Board, CreateBoardInput } from "./types";
