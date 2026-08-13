export interface RequestFail {
    error: {
        code: number,
        message: string,
        extra?: {
            error: string
        }
    }
}

export interface authResponse {
  token: string;
  userId: number;
}