function [precT, reclT] = precisionRecallPlot( score, label, varargin )

    [prec, recl] = precisionRecall( score, label );
    if nargin > 2
        plot( recl, prec, varargin{:} );
    end
    
    precT = zeros(3,1);
    reclT = [0.3, 0.5, 0.7];
    for j = 1:length(reclT)
        for tt = 1:length(recl)
            if recl(tt) == reclT(j)
                precT(j) = prec(tt);
            else if recl(tt) < reclT(j) && recl(tt+1) > reclT(j)
                    precT(j) = interpolate(recl(tt), recl(tt+1), prec(tt), prec(tt+1), reclT(j));
                    break;
                end
            end
        end
    end


    function [prec, recl] = precisionRecall( score, label )

        if length(score) ~= length(label)
            error('length of score and label does not match.')
        end

        n = length(score);
        data = zeros(n, 2);
        data(:,1) = score;
        data(:,2) = label;
                
        % randomize the vector first
        ind = randperm( length(score) );
        data = data(ind,:);

        
        [~, ind] = sort(data(:,1), 'descend');
        data = data(ind, :);

        step = max( round(n / 100), 1);
        totalPos = sum(data(:,2)==1);

        prec = zeros(1,1);
        recl = zeros(1,1);

        ind = 1;
        prec(ind) = 1;
        recl(ind) = 0;
        ind = ind + 1;
        for i = 1:step:n
            d = data(1:i,:);
            npos = sum(d(:,2)==1);
            prec(ind) = npos / i;
            recl(ind) = npos / totalPos;
            ind = ind + 1;
        end
    end
    
    function [prec] = interpolate( reca, recb, prea, preb, rect )
        s = (rect - reca) / (recb - reca);
        prec = prea + s * (preb - prea);
    end
        


end