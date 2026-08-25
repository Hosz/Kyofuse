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
  userId: string;
  email: string;
  username: string;
  role: string;
}
